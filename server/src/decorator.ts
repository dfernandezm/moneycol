import { ElasticSearchService } from './infrastructure/ElasticSearchService';
import { CollectionItemResult } from './infrastructure/collections/types';

const elasticService = new ElasticSearchService();

const decorateItems = async (language: string = "en", items: CollectionItemResult[]) => {
    return elasticService.decorateUsingIds(language, items.map(item => item.itemId));
}


const decorator = {
    decorateItems
}

export default decorator;