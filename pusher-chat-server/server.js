const express = require('express');
const bodyParser = require('body-parser');
const Pusher = require('pusher');
const mongoose = require('mongoose');

// יצירת האפליקציה של Express
const app = express();

// הגדרת middleware לקריאת נתונים ב־JSON
app.use(bodyParser.json());

// חיבור למסד נתונים MongoDB
mongoose.connect(process.env.MONGO_URI)
  .then(() => console.log('Connected to MongoDB'))
  .catch(err => console.error('Failed to connect to MongoDB:', err));

// יצירת מודל להודעות במסד הנתונים
const messageSchema = new mongoose.Schema({
  chatId: String,
  message: String,
  timestamp: { type: Date, default: Date.now },
});

const Message = mongoose.model('Message', messageSchema);

// יצירת אובייקט Pusher עם הפרטים שלך
const pusher = new Pusher({
  appId: '1935180',            // ה-App ID שלך
  key: '1a9bf825ae43161ae6c6', // ה-App Key שלך
  secret: '11bb53feb48ce269e15b', // ה-App Secret שלך
  cluster: 'mt1',              // ה-cluster שלך
  useTLS: true
});

// endpoint לשליחת הודעה
app.post('/message', async (req, res) => {
  const { message, chatId, userId } = req.body;  // מקבל את ההודעה, ה-chatId ו-userId

  // שמירת ההודעה במסד הנתונים תחת ה-chatId
  const newMessage = new Message({ message, chatId, userId });
  await newMessage.save();

  // שולח את ההודעה ל-Pusher
  pusher.trigger('chat-channel', 'new-message', { message, chatId, userId });
  console.log("Received message:", message);

  // שליחה של תשובה שההודעה נשלחה בהצלחה
  res.status(200).send('Message sent!');
});

// endpoint לשליפת כל ההודעות לפי chatId
app.get('/messages/:chatId', async (req, res) => {
  const { chatId } = req.params;  // מקבל את ה-chatId מתוך ה-URL

  try {
    // שליפה של כל ההודעות מהמסד נתונים לפי chatId
    const messages = await Message.find({ chatId }).sort({ timestamp: 1 });
    res.json(messages);
  } catch (err) {
    res.status(500).send('Error fetching messages');
  }
});
// הרצת השרת על פורט 3000
app.listen(3000, () => {
  console.log('Server is running on port 3000');
});