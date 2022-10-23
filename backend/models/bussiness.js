import { Schema } from "mongoose";
import mongoose from "mongoose";
import { Product } from "./product.js";
import { Rating } from "./rating.js";

const Bussiness = mongoose.model(
  "bussiness",
  new Schema({
    user: { type: Schema.Types.ObjectId },
    name: String,
    google_maps: String,
    category: Number,
    visited: Number,
    images: [String],
    icon: String,
    ratings: [Rating],
    menu: [Product]
  })
);

export { Bussiness };
