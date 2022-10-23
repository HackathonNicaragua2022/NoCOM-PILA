// Setting Server Listener
const port = 3000;
//const host = "192.168.1.22";
const host = "192.168.43.104";

import express from 'express';
import session from 'express-session';
import passport from 'passport';

import cors from 'cors';
import fs from "fs";
import formidable from "formidable";

import { connectMongoDB } from './database.js';
import {User} from './models/user.js';
import {Bussiness} from './models/bussiness.js';

import { Strategy } from "passport-local";
import { v4 as uuid } from "uuid";

const isAuthenticated = (req, res, next) => {
    if(req.isAuthenticated()) {
        return next();
    }
    res.status(401).send("Unauthorized");
}

const UploadImageFile = (form, req) =>
  new Promise((resolve, reject) => {
    form.parse(req, (err, fields, files) => {
      if (err) reject(err);
      else resolve(files.image);
    });
  });

const getRating = (buss) => {
  if (buss.ratings.length === 0) {
    return 5;
  }
  let total = 0;
  for (let i = 0; i < buss.ratings.length; i++) {
    total += buss.ratings[i].stars;
  }
  return total / buss.ratings.length;
};

const app = express();

// authentication section
{
  passport.use(
    "login",
    new Strategy(
      { usernameField: "email", passwordField: "password" },
      async (username, password, done) => {
        try {
          let user = await User.findOne({email: username});
          if (user) {
            if (user.password === password) {
              return done(null, {
                id: user.id,
                property: user.property
              });
            } else {
              return done(null, false, { message: "password_inv" });
            }
          } else {
            return done(null, false, { message: "user_inv" });
          }
        } catch (err) {
          console.log("server error: " + err);
          return done(err);
        }
      }
    )
  );
  
  passport.use(
    "signup",
    new Strategy(
      { usernameField: "email", passwordField: "password" },
      async (username, password, done) => {
        try {
          if (await User.findOne({email: username}) != undefined
          ) {
            return done(null, false, { message: "user_exist" });
          }
          const user = new User({
            name: "",
            phone: "",
            location: 0,
            email: username,
            password: password,
            property: false,
            image: ''
          })
          await user.save();
          return done(null, {
            id: user._id
          });
        } catch (err) {
          console.log("Server Error: " + err);
          return done(err);
        }
      }
    )
  );
  
  passport.serializeUser((user, done) => {
    // Almacenar sesion
    return done(null, user.id);
  });
  
  passport.deserializeUser(async (id, done) => {
    // Almacenar sesion
    try {
      let user = await User.findById(id);
      if (user) {
        delete user.password;
        done(null, user);
      }
    } catch (err) {
      console.log("Server Error: " + err);
      done(err);
    }
  });
}

app.use(cors());
app.use(express.urlencoded({extended:true, limit:'1024mb'}));
app.use(express.json({limit:'2048mb'}));

app.use(
  session({
    secret: "fss-steward-secret",
    name: "PHP_SID",
    resave: false,
    saveUninitialized: false,
    cookie: {
      maxAge: 5 * 60 * 60 * 1000, // session finish in 4 hours
    },
  })
);

app.use(passport.initialize());
app.use(passport.session());

app.use(express.static('uploads'));

app.post("/login",(req, res, next) => {
    passport.authenticate("login", function (err, user, info) {
      if (err) {
        console.log(err);
        return next(err);
      }
      if (!user) {
        return res.status(200).send(info);
      }
      req.logIn(user, function (err) {
        if (err) {
          console.log(err);
          return next(err);
        }
        return res
          .status(200)
          .send({
            message: "done",
            property: user.property
          });
      });
    })(req, res, next);
  });

app.post("/signin",(req, res, next) => {
    passport.authenticate("signup", async function (err, user, info) {
      if (err) {
        console.log(err);
        return next(err);
      }
      if (!user) {
        return res.status(200).send(info);
      }
      const user_ = await User.findById(user.id);
      user_.name = req.body.name;
      user_.property = req.body.property;
      user_.location = req.body.location;
      user_.phone = req.body.phone;
      user_.image = req.body.image;
      await user_.save();
      req.logIn(user, function (err) {
        if (err) {
          console.log(err);
          return next(err);
        }
        res.status(200)
          .send({
            message: "done",
            property: user_.property
          });
      });
    })(req, res, next);
  });

app.post("/rate-buss", isAuthenticated, async (req, res) => {
  const buss = await Bussiness.findById(req.body.id);
  buss.ratings.push({
    user: req.user._id,
    name: req.user.name,
    text: req.body.text,
    stars: req.body.stars
});
    res.status(200).send({});
});

app.post("/publish-buss",isAuthenticated,async (req, res) => {
    const buss = new Bussiness({
        name: req.body.name,
        user: req.user._id,
        category: req.body.category,
        google_maps: req.body.google_maps,
        images: req.body.images,
        icon: req.body.icon,
        menu: req.body.menu,
        visited: 0
    });
    await buss.save();
    res.status(200).send({});
});

app.post("/edit-buss",isAuthenticated, async (req, res) => {
  const buss = await Bussiness.findById(req.body.id);
  buss.name = req.body.name;
  buss.category = req.body.category;
  buss.google_maps = req.body.google_maps;
  buss.images = req.body.images;
  buss.menu = req.body.menu;
  buss.icon = req.body.icon;
  await buss.save();
  res.status(200).send({});
});

app.get("/delete-buss",isAuthenticated,async (req, res) => {
  await Bussiness.findByIdAndDelete(req.body.id);
  res.status(200).send({});
});

app.get("/get-user-info",isAuthenticated, (req, res) => {
  res.status(200).send(req.user);
});

app.get("/get-list-buss",isAuthenticated,async (req, res) => {
  let dest = [];
  const buss = await Bussiness.find();
  for(let i in buss) {
    dest.push({
      id: buss[i]._id,
      user: buss[i].user,
      name: buss[i].name,
      category: buss[i].category,
      icon: buss[i].icon,
      visited: buss[i].visited,
      rating: getRating(buss[i])
    });
  }
  res.status(200).send(dest);
});

app.post("/get-details-buss", isAuthenticated, async (req, res) => {
  const buss = await Bussiness.findById(req.body.id);
  let {name,user,category,icon,images,ratings,google_maps,menu} = buss;
  let prop = await User.findById(user);
  res.status(200).send({
    name,
    property_name: prop.name,
    property_phone: prop.phone,
    category,
    icon,
    images,
    rating: getRating(buss),
    ratings,
    google_maps,
    menu
  });
});

// check autentication status
app.get("/test",isAuthenticated,(req, res) => {
  res.send({
      result: 'done'
  });
});

app.get("/logout",isAuthenticated,(req, res) => {
  req.logout(function (err) {
    if (err) {
      return next(err);
    }
    res.status(200).send({
      message: "done",
    });
  });
});

app.post("/upload-image", async (req, res) => {
  if (fs.existsSync("./uploads") == false) {
    fs.mkdirSync("./uploads");
  }
  let form = formidable({
    uploadDir: "./uploads",
    keepExtensions: true,
    maxFileSize: 1024 * 1024 * 5, // max 3 MB
  });
  let image = await UploadImageFile(form, req);
  let id = uuid();
  let sp = image.newFilename.split(".");
  fs.renameSync("./uploads/" + image.newFilename, "./uploads/" + id + "." + sp[1]);
  res.status(200).send({ id });
});

(async () => {
  if(!fs.existsSync("./uploads")) {
    fs.mkdirSync("./uploads");
  }
  await connectMongoDB();
  app.listen(port,host,() => {
    console.log("Server running\nEndpoint: http://"+host+":"+port);
});
})();

