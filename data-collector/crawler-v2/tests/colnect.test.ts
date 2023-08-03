import * as cheerio from 'cheerio';
import fs from 'fs';
import path from 'path';
import { Cheerio, Element } from 'cheerio';
import { banknoteDetail } from '../src/colnect';
import { Banknote } from '../src/banknote';

test('cheerio load', async () => {
    const $ = cheerio.load('<html><body><h1>Hello, world!</h1></body></html>');
    expect($('h1').text()).toBe('Hello, world!');
});

test('all countries', async () => {
    const htmlContents = readHtmlFileToString('all-countries.html');
    const $ = cheerio.load(htmlContents);
    expect($('div.country a').length).toEqual(311);

});

test('single country with single page', async () => {

    const htmlContents = readHtmlFileToString('single-page-country.html');
    const $ = cheerio.load(htmlContents);

    let bankNoteDetails: Cheerio<Element> = $("#plist_items div.pl-it");
    let banknotes = await banknoteDetail($, bankNoteDetails);

    expect(banknotes.length).toBe(10);

    // https://codewithhugo.com/jest-array-object-match-contain/
    expect(banknotes[0]).toEqual<Banknote>(
        expect.objectContaining({ year: 1830 })
    );
})

const readHtmlFileToString = (htmlFilePath: string) => {
    return fs.readFileSync(path.resolve(__dirname, 'data') + '/' + htmlFilePath, 'utf8');
}