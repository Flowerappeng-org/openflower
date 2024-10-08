import { readYaml } from "../../common/util";
import _ from "lodash";
import path from "path";
import { OpenAPIV3, OpenAPI } from "openapi-types";
import { ConfigToType, DataSourcePlugin } from "lowcoder-sdk/dataSource";
import { runOpenApi } from "../openApi";
import { parseOpenApi, ParseOpenApiOptions } from "../openApi/parse";
import SwaggerParser from "@apidevtools/swagger-parser";

const spec = readYaml(path.join(__dirname, "./github.spec.yaml"));

const dataSourceConfig = {
  type: "dataSource",
  params: [
    {
      type: "textInput",
      key: "ApiKey.username",
      label: "Username",
      tooltip: "The username of your GitHub account.",
      placeholder: "<Your GitHub username>",
    },
    {
      type: "password",
      key: "ApiKey.password",
      label: "Password",
      tooltip:
        "[Document](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) about how to create a personal access token",
      placeholder: "<Your Personal Access Token>",
    },
  ],
} as const;

const parseOptions: ParseOpenApiOptions = {
  actionLabel: (method: string, path: string, operation: OpenAPI.Operation) => {
    return _.upperFirst(operation.operationId || "");
  },
};
const deRefedSpec = SwaggerParser.dereference(spec);

type DataSourceConfigType = ConfigToType<typeof dataSourceConfig>;

const gitHubPlugin: DataSourcePlugin<any, DataSourceConfigType> = {
  id: "github",
  name: "GitHub",
  icon: "github.svg",
  category: "App Development",
  dataSourceConfig,
  queryConfig: async () => {
    const { actions, categories } = await parseOpenApi(spec, parseOptions);
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
  run: async function (actionData, dataSourceConfig, ctx): Promise<any> {
    const runApiDsConfig = {
      url: "",
      serverURL: "https://api.github.com",
      dynamicParamsConfig: dataSourceConfig,
    };
    return runOpenApi(actionData, runApiDsConfig, spec as OpenAPIV3.Document, undefined, await deRefedSpec);
  },
};

export default gitHubPlugin;
