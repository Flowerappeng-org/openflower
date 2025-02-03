import { CompAction } from "lowcoder-core";
import { ChartCompChildrenType  } from "./treemapChartConstants";
import {
  hiddenPropertyView,
  Section,
  sectionNames,
} from "lowcoder-sdk";
import { trans } from "i18n/comps";
import { examplesUrl,optionUrl } from "../chartComp/chartConfigs/chartUrls";

export function treeChartPropertyView(
  children: ChartCompChildrenType,
  dispatch: (action: CompAction) => void
) {

  const jsonModePropertyView = (
    <>
      <Section name={trans("chart.config")}>
        {children.echartsData.propertyView({ label: trans("chart.data") })}

        {children.echartsTitleConfig.getPropertyView()}
        {children.echartsTitleVerticalConfig.getPropertyView()}
        {children.echartsTitle.propertyView({ label: trans("treeChart.title"), tooltip: trans("echarts.titleTooltip") })}

        {children.left.propertyView({ label: trans("treemapChart.left"), tooltip: trans("echarts.leftTooltip") })}
        {children.right.propertyView({ label: trans("treemapChart.right"), tooltip: trans("echarts.rightTooltip") })}
        {children.top.propertyView({ label: trans("treemapChart.top"), tooltip: trans("echarts.topTooltip") })}
        {children.bottom.propertyView({ label: trans("treemapChart.bottom"), tooltip: trans("echarts.bottomTooltip") })}

        {children.tooltip.propertyView({label: trans("treemapChart.tooltip"), tooltip: trans("echarts.tooltipTooltip") })}
        {children.labelVisibility.propertyView({label: trans("echarts.labelVisibility"), tooltip: trans("echarts.labelVisibilityTooltip") })}
      </Section>
      <Section name={sectionNames.interaction}>
        {children.onEvent.propertyView()}
      </Section>

      <Section name={sectionNames.chartStyle}>
        {children.chartStyle?.getPropertyView()}
      </Section>
      <Section name={sectionNames.titleStyle}>
        {children.titleStyle?.getPropertyView()}
      </Section>
      <Section name={sectionNames.detailStyle}>
        {children.detailStyle?.getPropertyView()}
      </Section>
      <Section name={sectionNames.layout}>{hiddenPropertyView(children)}</Section>
      <Section name={sectionNames.advanced}>
        {children.echartsOption.propertyView({
        label: trans("chart.echartsOptionLabel"),
        styleName: "higher",
        tooltip: (
          <div>
            <a href={optionUrl} target="_blank" rel="noopener noreferrer">
              {trans("chart.echartsOptionTooltip")}
            </a>
            <br />
            <a href={examplesUrl} target="_blank" rel="noopener noreferrer">
              {trans("chart.echartsOptionExamples")}
            </a>
          </div>
        ),
      })}
      </Section>
    </>
  );
  
  const getChatConfigByMode = (mode: string) => {
    switch(mode) {
      case "json":
        return jsonModePropertyView;
    }
  }
  return getChatConfigByMode(children.mode.getView())
}
