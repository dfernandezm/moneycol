const API = `http://localhost:4000/search?qs=%query%&from=%from%&size=%size%`;

//TODO: sanitize search term before sending (lowercase etc)
const searchApiCall = (searchTerm, from, size) => {
    let apiReplaced = API.replace("%query%", searchTerm);
    apiReplaced = apiReplaced.replace("%from%", from);
    apiReplaced = apiReplaced.replace("%size%", size);
    return fetch(apiReplaced)
              .then(response => response.json())
              .then(data => data.searchResults.results);
};

export default { searchApiCall };