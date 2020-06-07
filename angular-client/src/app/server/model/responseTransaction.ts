/**
 * Accounting
 * This is an accounting server. 
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { ResponseTransactionEntries } from './responseTransactionEntries';


export interface ResponseTransaction { 
    id?: number;
    date?: string;
    name?: string;
    entries?: Array<ResponseTransactionEntries>;
}

