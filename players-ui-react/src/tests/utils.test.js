import {validateId, validateCountryCode} from '../utils/index';

test('basic utility functions tests', () => {
    expect(validateId('aardsda01')).toBe(true);
    expect(validateId('')).toBe(false);
    expect(validateId('   ')).toBe(false);
    expect(validateId(null)).toBe(false);
    expect(validateCountryCode('USA')).toBe(true);
    expect(validateCountryCode('')).toBe(false);
    expect(validateCountryCode('   ')).toBe(false);
    expect(validateCountryCode(null)).toBe(false);
});

