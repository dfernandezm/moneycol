import React from 'react';

import { SearchResult } from './types/SearchResult';

import { makeStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import CardMedia from '@material-ui/core/CardMedia';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';

import Grid from '@material-ui/core/Grid';


const useStyles = makeStyles({
  card: {
    width: "100%",
    marginBottom: 10
  },
});

type SearchResultsItemProps = {
    item: SearchResult,
    index: number
}

const SearchResultItem: React.FC<SearchResultsItemProps> = ({item, index}) => {
  const classes = useStyles();
  return (
    <Card className={classes.card}>
      
        <Grid container spacing={3}>
            <Grid item xs={6}>
                <CardMedia
                    component="img"
                    alt="image"
                    height="200"
                    image={item.imageFront}
                    title="image"
                />
            </Grid>
            <Grid item xs={6}>
                <CardContent>
                    <Typography gutterBottom variant="h4" component="h2">
                        {item.country} - {item.banknoteName}
                    </Typography>
                    <Typography gutterBottom variant="h5" component="h2">
                        {item.year}
                    </Typography>
                    <Typography variant="body2" color="textSecondary" component="p">
                        {item.description}
                    </Typography>
                    <Typography variant="body2" color="textSecondary" component="p">
                        <a href={item.detailLink} target="_blank">Detail</a>
                    </Typography>
                </CardContent>
            </Grid>
            <Grid item xs={12}>
                <CardActions>
                    <Button size="small" color="primary">
                    Share
                    </Button>
                    <Button size="small" color="primary">
                    Add to Collection
                    </Button>
                </CardActions>
            </Grid>
        </Grid>
    </Card>
  );
}

export default SearchResultItem;

