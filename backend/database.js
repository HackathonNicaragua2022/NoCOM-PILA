import fs from "fs";

const beginDatabase = () => {
  if (fs.existsSync("./database.json") === false) {
    fs.writeFileSync("./database.json", JSON.stringify({bussiness:[], users:[]}));
  }
  return JSON.parse(fs.readFileSync("./database.json", { encoding: "utf-8" }));
};

const endDatabase = (db) => {
  fs.writeFileSync("./database.json", JSON.stringify(db));
};

export { beginDatabase, endDatabase };
