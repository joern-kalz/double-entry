import { Directive, Input } from "@angular/core";

@Directive({
    selector: '[baseChart]'
})
export class BaseChartDirectiveStub {
    @Input() datasets: any;
    @Input() data: any;
    @Input() labels: any;
    @Input() options: any;
    @Input() chartType: any;
}