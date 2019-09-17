import { IResolvers } from 'graphql-tools';
//https://www.compose.com/articles/use-all-the-databases-part-2/#elasticsearch
const resolverMap: IResolvers = {
    Query: {
        helloWorld(_: void, args: void): string {
            return `👋 Hello world! 👋`;
        },
    },
    SearchQuery: {
        async search(_, args: void): Promise<any> {
            
        }
    }
    
};

export default resolverMap;