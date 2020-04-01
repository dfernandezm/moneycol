import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import Container from '@material-ui/core/Container';


import { useQuery } from '@apollo/react-hooks';
import { COLLECTIONS_FOR_COLLECTOR } from './gql/collectionsForCollector';
import { BankNoteCollection } from './types';

const useStyles = makeStyles({
    table: {
        minWidth: 650,
    },
});

interface Props {
    collector: string
}

const CollectionsTable: React.FC<Props> = ({ collector }) => {
    const classes = useStyles();
    const { data, loading, error } = useQuery(COLLECTIONS_FOR_COLLECTOR, {
        variables: { collectorId: collector },
    });

    if (loading) return <p>Loading</p>;
    if (error) return <p>Error: {error}</p>;
    return (
        <Container maxWidth="lg">
        <TableContainer component={Paper}>
            <Table className={classes.table} aria-label="simple table">
                <TableHead>
                    <TableRow>
                        <TableCell>Name</TableCell>
                        <TableCell align="right">Description</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {data.collections.map((collection: BankNoteCollection) => (
                        <TableRow key={collection.name}>
                            <TableCell component="th" scope="row">
                                {collection.name}
                            </TableCell>
                            <TableCell align="right">{collection.description}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
        </Container>

    )
}

export { CollectionsTable };