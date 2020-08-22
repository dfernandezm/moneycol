export type CollectionApiResult = {
    id: string,
    name: string,
    description: string,
    collectorId: string,
    items: CollectionItemResult[] 
}

export type CollectionItemResult = {
    itemId: string
}
