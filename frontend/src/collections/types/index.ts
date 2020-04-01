export type BankNoteCollection = {
   collectionId: string
   name: string
   description: string
   bankNotes: BankNote[]
}

export type BankNote = {
    country: string
    banknoteName: string
    year: number
}