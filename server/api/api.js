const express = require('express')
const app = express()
const port = 4000

const searcher = require("../infrastructure/search");
const userModel = require("../infrastructure/user");
const bodyParser = require('body-parser'); 

app.use(bodyParser.urlencoded({
    extended: true
}));

app.use(bodyParser.json());

app.use(function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    res.header("Access-Control-Allow-Methods", "GET,POST,OPTIONS,HEAD");
    if ('OPTIONS' === req.method) {
      //respond with 200
      res.send(200);
    }
    else {
    //move on
      next();
    }
  });

app.get('/search', async (req, res) => {
    const searchQuery = req.query.qs;
    const from = req.query.from;
    const size = req.query.size;
    console.log('SearchQuery: ' + searchQuery);
    const searchResults =  await searcher.search("en", searchQuery, from, size);
    console.log("Results: " + searchResults);        
    const json = { searchResults };
    res.send(json);
});

//TODO: error handler did not fire when 'req.body() is not a function' fired, need to catch here
app.post('/users', async (req, res) => {
  const reqBody = req.body;
  console.log("User to add:", JSON.stringify(reqBody));
  const user = await userModel.registerUser(reqBody);
  res.send({message: "User added"});
});

// error handler
app.use(function (err, req, res, next) {
    console.error(err.stack)
    const errorMessage = { message: err.message};
    res.status(500).send(errorMessage);
  })

app.listen(port, () => console.log(`Example app listening on port ${port}!`))