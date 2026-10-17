import fs from "node:fs";
import path from "node:path";
import { environment } from "./supabase-env.mjs";
const dir = process.argv[2] || "dist-live",
  env = environment();
function files(p) {
  return fs
    .readdirSync(p, { withFileTypes: true })
    .flatMap((e) =>
      e.isDirectory() ? files(path.join(p, e.name)) : [path.join(p, e.name)],
    );
}
const assets = files(dir).filter((p) => /\.(js|html)$/.test(p));
const content = assets.map((p) => fs.readFileSync(p, "utf8")).join("\n");
for (const marker of [
  "arivihan.demo.v1",
  "The metre is the SI base unit of length.",
  "Divide centimetres by 100: 250 / 100 = 2.5 m.",
])
  if (content.includes(marker))
    throw Error("Live bundle contains local mock storage/grading material.");
for (const [name, value] of Object.entries(env)) {
  if (
    /SUPABASE.*(ACCESS_TOKEN|SERVICE_ROLE|SECRET|DB_URL)|DATABASE_URL|PGPASSWORD/.test(
      name,
    ) &&
    value &&
    content.includes(value)
  )
    throw Error(
      "A server credential appears in the live bundle; contents withheld.",
    );
}
console.log(
  "PASS live bundle: local mock grading/storage excluded; no configured privileged credential values found.",
);
