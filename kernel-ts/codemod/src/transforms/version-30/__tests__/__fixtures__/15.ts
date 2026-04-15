// description: preferTopLevelImport - kernel-md-facade

import { TypeC } from "@com.mgmtp.a12.kernel/kernel-md-facade"; // top-level import --> should not change
import { TypeE } from "@com.mgmtp.a12.kernel/kernel-md-facade/lib/main/js/a12internal/index.js"; // a12internal import
import { TypeB } from "@com.mgmtp.a12.kernel/kernel-md-facade/lib/main/js/formatdef/error/keys.js"; // named import (deep)
import { TypeA } from "@com.mgmtp.a12.kernel/kernel-md-facade/lib/main/js/index.js"; // named import (main index.js)
import { TypeF } from "@com.mgmtp.a12.kernel/kernel-md-facade/lib/main/js/internal/yolo/SomeInternalUtil.js"; // internal import
import * as TypeD from "@com.mgmtp.a12.kernel/kernel-md-facade/lib/main/js/some/module"; // namespace import
import { OtherKernel } from "@com.mgmtp.a12.kernel/kernel-other-package/lib/main/js/index"; // other kernel package --> should not change
import { External } from "some-external-package/deep/path"; // other external package --> should not change
export * from "@com.mgmtp.a12.kernel/kernel-md-facade/lib/main/js/some/deep/path"; // re-export

const a: TypeA = {};
const b: TypeB = {};
const c: TypeC = {};
const d = TypeD.something;
const e: TypeE = {};
const f: TypeF = {};

const external: External = {};
const otherKernel: OtherKernel = {};