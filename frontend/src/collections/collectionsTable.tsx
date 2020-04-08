import React from 'react';
import { withStyles, Theme, createStyles, makeStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';

import { useQuery } from '@apollo/react-hooks';
import { COLLECTIONS_FOR_COLLECTOR } from './gql/collectionsForCollector';
import { BankNoteCollection } from './types';

const StyledTableCell = withStyles((theme: Theme) =>
    createStyles({
        head: {
            backgroundColor: theme.palette.common.black,
            color: theme.palette.common.white,
        },
        body: {
            fontSize: 14,
        },
    }),
)(TableCell);

const StyledTableRow = withStyles((theme: Theme) =>
        createStyles({
            root: {
                '&:nth-of-type(odd)': {
                    backgroundColor: theme.palette.background.default,
                },
                border: 'none'
            },
        }),
    )(TableRow);

    const StyledTable = withStyles({
        root: {
            border: 'none'
        }
    })(TableRow);

interface Props {
    collector: string
}

const useStyles = makeStyles({
    table: {
        minWidth: 700,
    },
    th: {
        border: 'none',
        borderBottom: '1px solid #fafafa'
    }
});

const CollectionsTable: React.FC<Props> = ({ collector }) => {
    const { data, loading, error } = useQuery(COLLECTIONS_FOR_COLLECTOR, {
        variables: { collectorId: collector },
    });

    const classes = useStyles();

    if (loading) return <p>Loading</p>;
    if (error) return <p>Error: {error}</p>;
    return (

        <TableContainer component={Paper}>
            <StyledTable className={classes.table} aria-label="simple table">
                <TableHead className={classes.th}>
                    <TableRow>
                        <TableCell>Name</TableCell>
                        <TableCell align="right">Description</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {data.collections.map((collection: BankNoteCollection) => (
                        <StyledTableRow key={collection.name}>
                            <StyledTableCell component="th" scope="row">
                                {collection.name}
                            </StyledTableCell>
                            <StyledTableCell align="right">{collection.description}</StyledTableCell>
                        </StyledTableRow>
                    ))}
                </TableBody>
            </StyledTable>
        </TableContainer>
    )
}

export { CollectionsTable };