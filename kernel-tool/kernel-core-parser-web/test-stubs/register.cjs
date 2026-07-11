const { register } = require("node:module");
const { pathToFileURL } = require("node:url");
const { resolve } = require("node:path");

register(pathToFileURL(resolve(__dirname, "./loader.mjs")).href);
