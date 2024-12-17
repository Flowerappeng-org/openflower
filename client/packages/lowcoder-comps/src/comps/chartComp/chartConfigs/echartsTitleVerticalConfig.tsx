import {
  AlignBottom,
  AlignTop,
  dropdownControl,
  MultiCompBuilder,
} from "lowcoder-sdk";
import { LegendComponentOption } from "echarts";
import { trans } from "i18n/comps";

const LegendPositionOptions = [
  {
    label: <AlignBottom />,
    value: "bottom",
  },
  {
    label: <AlignTop />,
    value: "top",
  },
] as const;

export const EchartsTitleVerticalConfig = (function () {
  return new MultiCompBuilder(
    {
      top: dropdownControl(LegendPositionOptions, "top"),
    },
    (props): LegendComponentOption => {
      const config: LegendComponentOption = {
        top: "top",
        type: "scroll",
      };
      config.top = props.top
      return config;
    }
  )
    .setPropertyViewFn((children) => (
      <>
        {children.top.propertyView({
          label: trans("echarts.titlePositionVertical"),
          radioButton: true,
        })}
      </>
    ))
    .build();
})();
