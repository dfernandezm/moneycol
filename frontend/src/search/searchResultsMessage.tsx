import React from 'react';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';

type SearchResultsMessage = {
  message?: string,
  searchTerm: string
}

const useStyles = makeStyles({
  resultsMessage: {
    width: "100%",
    marginBottom: 10,
    marginTop: 10
  },
});

const SearchResultsMessage: React.FC<SearchResultsMessage> = ({ message = "Search results for", searchTerm }) => {
  const classes = useStyles()
  return (
    <Typography gutterBottom variant="h6" component="p" className={classes.resultsMessage}>
      {message}: {searchTerm}
    </Typography>
  )
}

export default SearchResultsMessage;

