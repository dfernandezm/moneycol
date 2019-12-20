import React from 'react';
import { Theme, createStyles, makeStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Container from '@material-ui/core/Container';
import Typography from '@material-ui/core/Typography';

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      padding: theme.spacing(3, 2),
      boxShadow: "none",
    },
  }),
);

interface EmptyResultsProps {
  message?: string
}

const EmptySearchResults: React.FC<EmptyResultsProps> = ({ message = "No results to show" }) => {
  const classes = useStyles()
  return (
    <Container
      maxWidth="md">
      <Paper className={classes.root}>
        <Typography variant="h5" component="h3">
          {message}
        </Typography>
        <Typography component="p">
          Try refining your search
          </Typography>
      </Paper>
    </Container>
  )
}

export default EmptySearchResults;
