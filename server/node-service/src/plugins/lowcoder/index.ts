import { readYaml } from "../../common/util";
import _ from "lodash";
import path from "path";
import { OpenAPIV3, OpenAPI } from "openapi-types";
import { ConfigToType, DataSourcePlugin } from "lowcoder-sdk/dataSource";
import { runOpenApi } from "../openApi";
import { parseOpenApi, ParseOpenApiOptions } from "../openApi/parse";

import spec from './lowcoder.spec.json';

const dataSourceConfig = {
  type: "dataSource",
  params: [
    {
      key: "serverURL",
      type: "textInput",
      label: "Lowcoder API Service URL",
      rules: [{ required: true }],
      placeholder: "https://<your-lowcoder-api-service>:port",
      tooltip: "Input the server url of your self-hosting instance or api-service.lowcoder.cloud if you are running your apps on the free public Community Edition Cloud Service.",
    },
    {
      "type": "groupTitle",
      "key": "API Key",
      "label": "Api Key Auth"
    },
    {
      type: "password",
      key: "bearerAuth.value",
      label: "Authorization",
      "tooltip": "API Key Authentication with a Bearer token. Copy your API Key here. (e.g. 'Bearer eyJhbGciO...')",
      "placeholder": "API Key Authentication with a Bearer token. Copy your API Key here. (e.g. 'Bearer eyJhbGciO...')"
    },
    {
      label: "Spec Version",
      key: "specVersion",
      type: "select",
      tooltip: "Version of the spec file.",
      placeholder: "v1.0",
      options: [
        {
          value: "v1.0",
          label: "v1.0",
        },
        {
          value: "v2.0",
          label: "v2.0",
        }
      ]
    },
]
} as const;

const parseOptions: ParseOpenApiOptions = {
  actionLabel: (method: string, path: string, operation: OpenAPI.Operation) => {
    return _.upperFirst(operation.operationId || "");
  },
};

type DataSourceConfigType = ConfigToType<typeof dataSourceConfig>;

const lowcoderPlugin: DataSourcePlugin<any, DataSourceConfigType> = {
  id: "lowcoder",
  name: "Lowcoder API",
  icon: "lowcoder.svg",
  category: "api",
  dataSourceConfig,
  queryConfig: async () => {
    const { actions, categories } = await parseOpenApi(spec as unknown as OpenAPI.Document, parseOptions);
    return {
      type: "query",
      label: "Action",
      categories: {
        label: "Resources",
        items: categories,
      },
      actions,
    };
  },
  run: function (actionData, dataSourceConfig): Promise<any> {
    const { serverURL, ...otherDataSourceConfig } = dataSourceConfig;
    console.log("Lowcoder API Plugin: run", serverURL, otherDataSourceConfig);
    const runApiDsConfig = {
      url: "",
      serverURL: serverURL,
      dynamicParamsConfig: otherDataSourceConfig,
      specVersion: dataSourceConfig.specVersion,
    };
    return runOpenApi(actionData, runApiDsConfig, spec as OpenAPIV3.Document);
  },
};

export default lowcoderPlugin;
