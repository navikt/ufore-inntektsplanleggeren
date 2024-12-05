import { describe, it, expect } from 'vitest';
import { isInntekterPayload, isInntektObject, hasInntektValue, isInntektValue } from './validators';

describe('validators', () => {
    describe('isInntekterPayload', () => {
        it('should return false for null data', () => {
            expect(isInntekterPayload(null)).toBe(false);
        });

        it('should return false for non-object data', () => {
            expect(isInntekterPayload(123)).toBe(false);
            expect(isInntekterPayload('string')).toBe(false);
        });

        it('should return false if required keys are missing', () => {
            expect(isInntekterPayload({})).toBe(false);
            expect(isInntekterPayload({ year: 2023 })).toBe(false);
            expect(isInntekterPayload({ brukerInntekter: {}, epsInntekter: {} })).toBe(false);
        });

        it('should return true for valid data', () => {
            const validData = {
                year: 2023,
                brukerInntekter: {
                    arbeidsinntekt: 1000,
                    andrePensjonsgivendeYtelser: 500,
                    naeringsinntekt: 200,
                    inntektUtland: 300,
                    pensjonUtland: 400
                },
                epsInntekter: {
                    arbeidsinntekt: 1000,
                    andrePensjonsgivendeYtelser: 500,
                    naeringsinntekt: 200,
                    inntektUtland: 300,
                    pensjonUtland: 400
                }
            };
            expect(isInntekterPayload(validData)).toBe(true);
        });
    });

    it("should return true for valid data with some null values", () => {
        const validData = {
            brukerInntekter: {
                arbeidsinntekt: null,
                andrePensjonsgivendeYtelser: 22144,
                naeringsinntekt: 23543,
                inntektUtland: null,
                pensjonUtland: null
            }, epsInntekter: {
                arbeidsinntekt: 10,
                andrePensjonsgivendeYtelser: 10,
                naeringsinntekt: 2341024,
                inntektUtland: 4553,
                pensjonUtland: 3323
            },
            year: 2024
        }
        expect(isInntekterPayload(validData)).toBe(true);
    });

    it("should return true for valid data with only null values", () => {
        const validData = {
            brukerInntekter: {
                arbeidsinntekt: null,
                andrePensjonsgivendeYtelser: null,
                naeringsinntekt: null,
                inntektUtland: null,
                pensjonUtland: null
            }, epsInntekter: {
                arbeidsinntekt: null,
                andrePensjonsgivendeYtelser: null,
                naeringsinntekt: null,
                inntektUtland: null,
                pensjonUtland: null
            },
            year: 2024
        }
        expect(isInntekterPayload(validData)).toBe(true);
    });

    describe('isInntektObject', () => {
        it('should return false for null data', () => {
            expect(isInntektObject(null)).toBe(false);
        });

        it('should return false if required keys are missing', () => {
            expect(isInntektObject({})).toBe(false);
            expect(isInntektObject({ arbeidsinntekt: 1000 })).toBe(false);
        });

        it('should return true for valid data', () => {
            const validData = {
                arbeidsinntekt: 1000,
                andrePensjonsgivendeYtelser: 500,
                naeringsinntekt: 200,
                inntektUtland: 300,
                pensjonUtland: 400
            };
            expect(isInntektObject(validData)).toBe(true);
        });
    });

    describe('hasInntektValue', () => {
        it('should return false if key is missing', () => {
            expect(hasInntektValue({}, 'arbeidsinntekt')).toBe(false);
        });

        it('should return false if value is invalid', () => {
            expect(hasInntektValue({ arbeidsinntekt: -1 }, 'arbeidsinntekt')).toBe(false);
        });

        it('should return true for valid value', () => {
            expect(hasInntektValue({ arbeidsinntekt: 1000 }, 'arbeidsinntekt')).toBe(true);
        });
    });

    describe('isInntektValue', () => {
        it('should return true for null value', () => {
            expect(isInntektValue(null)).toBe(true);
        });

        it('should return false for negative number', () => {
            expect(isInntektValue(-1)).toBe(false);
        });

        it('should return true for non-negative number', () => {
            expect(isInntektValue(0)).toBe(true);
            expect(isInntektValue(1000)).toBe(true);
        });
    });
});