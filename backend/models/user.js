import { Schema } from 'mongoose';
import mongoose from 'mongoose';

const User = mongoose.model('user', new Schema({
    name: String,
    email: String,
    password: String,
    phone: String,
    location: Number,
    image: String,
    property: Boolean
}));

export {
    User
}