export const validateId = (value) => {
    return Boolean(value && value.trim().length > 0);
}

export const validateCountryCode = (value) => {
    return Boolean(value && value.trim().length > 0);
}

export const sanitizeInput =  (value) => {
    return value;
}
