export class Converter {
  static parseApiAmount(value: string): number {
    return +value;
  }

  static formatApiAmount(value: number): string {
    return (Math.round(value * 100) / 100).toFixed(2);
  }
}
