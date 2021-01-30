import { Converter } from './converter';

describe('Converter', () => {
  it('should parse API amount string', () => {
    expect(Converter.parseApiAmount('12.34')).toEqual(12.34);
  });

  it('should format API amount string', () => {
    expect(Converter.formatApiAmount(12.34)).toEqual('12.34');
  });
});
