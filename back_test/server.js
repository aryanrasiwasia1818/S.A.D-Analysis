const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
const axios = require('axios'); // Used to interact with your AI model API or any other

const app = express();
app.use(cors());
const server = http.createServer(app);
const io = socketIo(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"]
  }
});

// Mock AI model function - Replace this with actual integration
const getBotResponse = async (message) => {
  // Example: Call to an LLM API (replace with your actual API)
  try {
    const response = await axios.post('https://api.your-llm.com/chat', {
      prompt: message,
    });
    return response.data.reply;  // Adjust based on your actual LLM response format
  } catch (error) {
    console.error('AI model error:', error);
    return "Sorry, I couldn't understand that.";
  }
};

io.on('connection', (socket) => {
  console.log('User connected:', socket.id);

  // Listen for messages from the client
  socket.on('chat message', async (data) => {
    console.log('Received message:', data.message);

    // Process the message (replace this with actual AI processing)
    const botReply = await getBotResponse(data.message);

    // Emit the bot's reply to the client
    io.emit('chat message', { message: botReply, sender: 'bot' });
  });

  // Handle user disconnection
  socket.on('disconnect', () => {
    console.log('User disconnected:', socket.id);
  });
});

// Test route
app.get('/', (req, res) => res.send('Chat server running!'));

// Start server
const PORT = process.env.PORT || 3000;
server.listen(PORT, () => console.log(`Server running on port ${PORT}`));

