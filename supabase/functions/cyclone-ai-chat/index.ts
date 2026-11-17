import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const { message, history } = await req.json()

    if (!message || typeof message !== 'string') {
      return new Response(
        JSON.stringify({ error: 'Message is required and must be a string.' }),
        { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    const geminiApiKey = Deno.env.get('GEMINI_API_KEY')
    if (!geminiApiKey) {
      return new Response(
        JSON.stringify({ error: 'GEMINI_API_KEY environment variable is not configured.' }),
        { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    // Build contents for Gemini API (supporting multi-turn history)
    const contents: Array<any> = []

    // Add system instruction as initial context or prepend to history
    contents.push({
      role: 'user',
      parts: [{ text: 'System Instruction: You are CycloneSafe AI Assistant, an expert emergency response and disaster preparedness advisor specializing in tropical cyclones, typhoons, and severe weather safety. Provide clear, calm, life-saving, and accurate advice on evacuation, emergency kits, shelter, wind and flood safety, and post-storm recovery. Be concise and structured.' }]
    })
    contents.push({
      role: 'model',
      parts: [{ text: 'Understood. I am ready to provide expert cyclone safety and emergency guidance as the CycloneSafe AI Assistant.' }]
    })

    if (history && Array.isArray(history)) {
      for (const h of history) {
        // Map frontend role ('user', 'model') to Gemini API format ('user', 'model')
        const role = h.role === 'model' || h.role === 'assistant' ? 'model' : 'user'
        contents.push({
          role: role,
          parts: [{ text: h.content }]
        })
      }
    } else {
      contents.push({
        role: 'user',
        parts: [{ text: message }]
      })
    }

    // Call Google Gemini API (gemini-1.5-flash)
    const geminiResponse = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${geminiApiKey}`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          contents: contents,
          generationConfig: {
            temperature: 0.7,
            maxOutputTokens: 1024,
          }
        }),
      }
    )

    if (!geminiResponse.ok) {
      const errText = await geminiResponse.text()
      throw new Error(`Gemini API error: ${geminiResponse.status} - ${errText}`)
    }

    const geminiData = await geminiResponse.json()
    const reply = geminiData.candidates?.[0]?.content?.parts?.[0]?.text || 'No response generated from Gemini.'

    // Optional: Save chat history to database if authenticated
    const authHeader = req.headers.get('Authorization')
    if (authHeader) {
      try {
        const supabaseUrl = Deno.env.get('SUPABASE_URL') ?? ''
        const supabaseAnonKey = Deno.env.get('SUPABASE_ANON_KEY') ?? ''
        const supabase = createClient(supabaseUrl, supabaseAnonKey, {
          global: { headers: { Authorization: authHeader } }
        })

        const { data: { user } } = await supabase.auth.getUser()
        if (user) {
          await supabase.from('chat_messages').insert([
            { user_id: user.id, role: 'user', content: message },
            { user_id: user.id, role: 'assistant', content: reply }
          ])
        }
      } catch (dbErr) {
        console.error('Failed to save chat history to database:', dbErr)
      }
    }

    return new Response(
      JSON.stringify({ response: reply }),
      { status: 200, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    )

  } catch (error) {
    return new Response(
      JSON.stringify({ error: error.message || 'Internal Server Error' }),
      { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    )
  }
})
