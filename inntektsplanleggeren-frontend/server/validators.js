/**
 * @param {object|null} data
 * @returns {boolean}
 */
export const isInntekterPayload = (data) => {
    if (data === null || typeof data !== 'object') {
        return false;
    }

    if (!Object.hasOwn(data, 'year') || !Object.hasOwn(data, 'brukerInntekter') || !Object.hasOwn(data, 'epsInntekter')) {
        return false;
    }

    return isInntektObject(data.brukerInntekter) && isInntektObject(data.epsInntekter);
};

/**
 * @param {object|null} data
 * @returns {boolean}
 */
export const isInntektObject = (data) => data !== null &&
    hasInntektValue(data, 'arbeidsinntekt') &&
    hasInntektValue(data, 'andrePensjonsgivendeYtelser') &&
    hasInntektValue(data, 'naeringsinntekt') &&
    hasInntektValue(data, 'inntektUtland') &&
    hasInntektValue(data, 'pensjonUtland');

/**
 * @param {Object} data
 * @param {string} key
 * @returns {boolean}
 */
export const hasInntektValue = (data, key) => Object.hasOwn(data, key) && isInntektValue(data[key]);

/**
 * @param {Object} data
 * @returns {boolean}
 */
export const isInntektValue = (data) => data === null || (typeof data === 'number' && data >= 0);