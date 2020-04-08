import React from 'react';
import { Typography, Container, Paper } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import { CollectionsTable } from "./collectionsTable";


const useStyles = makeStyles({
  "@global": {
    body: {
      backgroundColor: "#fff"
    }
  },
  paper: {
    marginTop: 10,
    display: "flex",
    padding: 20,
    flexDirection: "column",
    alignItems: "left"
  },
  title: {
    marginBottom: 20
  }
});


const CollectionsScreen: React.FC = () => {

  const classes = useStyles();

  return (
    <Container maxWidth="lg">
      <Paper className={classes.paper}>
        <Typography variant="h4" component="h4" className={classes.title}>
          My Collections
         </Typography>
        <CollectionsTable collector={"david9090"} />
      </Paper>
    </Container>
  )
}

export { CollectionsScreen };