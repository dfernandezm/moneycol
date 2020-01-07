import React, { useEffect, useState } from 'react';

import { SearchResult } from './types/SearchResult';

import { makeStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import CardMedia from '@material-ui/core/CardMedia';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';

import Grid from '@material-ui/core/Grid';

import { useMediaQuery } from '../hooks';


const useStyles = makeStyles({
    card: {
        width: "100%",
        marginBottom: 10,
        boxShadow: 'none',
        borderBottom: '1px solid #80808057',
    },
    square: {
        height: "auto",
        width: "auto"
    },
    imgStyle: {
        maxWidth: "100%",
        maxHeight: "100%"
    },
    imgFrame: {
        paddingTop: "16px"
    },
    alignText: {
        textAlign: 'right',
        justifyContent: 'flex-end'
    },
    floatingImages: {
        display: 'flex'
    }
});

type SearchResultsItemProps = {
    item: SearchResult,
    index: number
}

const SearchResultItem: React.FC<SearchResultsItemProps> = ({ item, index }) => {
    const classes = useStyles();
    const isSmall = useMediaQuery('(min-width: 650px)');

    const floatingImages = (
        <Grid container spacing={1} className="floatingImages">
            
            {item.imageBack !== 'https:undefined' ?
                <>
                <Grid item xs={6}>
                <div className={classes.square + ' ' + classes.imgFrame}>
                    <img src={item.imageFront} className={classes.imgStyle} />
                </div>
                </Grid>
                    <Grid item xs={6}>
                    <div className={classes.square + ' ' + classes.imgFrame}>
                        <img src={item.imageBack} className={classes.imgStyle} />
                    </div>
                </Grid> 
                </>
                :
                <Grid item xs={12}>
                    <div className={classes.square + ' ' + classes.imgFrame}>
                        <img src={item.imageFront} className={classes.imgStyle} />
                    </div>
                </Grid>
            }
        </Grid>
    )

    const stackedDivs = (
        <>
             <div className={classes.square + ' ' + classes.imgFrame}>
                <img src={item.imageFront} className={classes.imgStyle} />
            </div>
            {/* <div className={classes.square + ' ' + classes.imgFrame}>
                <img src={item.imageBack} className={classes.imgStyle} />
            </div>
            */}
        </>
    )

    return (
        <Card className={classes.card}>

            <Grid container spacing={2}>
                <Grid item xs={7} className={classes.imgFrame}>

                    {isSmall ? floatingImages : stackedDivs}

                </Grid>
                <Grid item xs={5}>
                    <CardContent>
                        <Typography gutterBottom variant="h6" component="h4">
                            {item.country} {item.banknoteName}, {item.year}
                        </Typography>
                        <Typography variant="body2" color="textSecondary" component="p">
                            {item.description}
                        </Typography>
                    </CardContent>
                </Grid>
                <Grid item xs={12}>
                    <CardActions className={classes.alignText}>
                        <Button size="small" color="primary" href={item.detailLink} target="_blank">
                           Detail
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

