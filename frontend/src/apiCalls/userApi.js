const API = 'http://localhost:4000/users';

//TODO: sanitize search term before sending (lowercase etc)
const postData = (url = ``, data = {}) => {
    // Default options are marked with *
      return fetch(url, {
          method: "POST", // *GET, POST, PUT, DELETE, etc.
          cache: "no-cache", // *default, no-cache, reload, force-cache, only-if-cached
          headers: {
              "Content-Type": "application/json",
          },
          body: JSON.stringify(data), // body data type must match "Content-Type" header
      })
      .then(response => response.json()) // check why when on error from server, there is a json parse error in console
      .catch(err => console.log(err));
  }

  const addUser = (user) => {
      console.log("Adding user");
      return postData(API, user);
  }

  export default { addUser };