import React from 'react';
import { CollectionsTable } from "./collectionsTable"; 

const CollectionsScreen: React.FC = () => {
    const sampleCollectorId = "13M99QaYnCZ2AKkwDo4YyMTw1ih1";
    return <CollectionsTable collector={sampleCollectorId} />
}

export { CollectionsScreen };