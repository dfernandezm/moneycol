//Consider: https://typegraphql.ml/
//https://www.compose.com/articles/use-all-the-databases-part-2/#elasticsearch

import { IResolvers } from 'graphql-tools';
import { SearchService } from './infrastructure/SearchService';
import { ElasticSearchService } from './infrastructure/ElasticSearchService';
import { SearchResult } from './infrastructure/SearchResult';

const searchService: SearchService = new ElasticSearchService();

const resolverMap: IResolvers = {
    Query: {
        async search(_: void, args: { term: string, from: number, to: number }, ctx): Promise<SearchResult> {
            return searchService.search("en", args.term, args.from, args.to);
        },
    }
};

export default resolverMap;