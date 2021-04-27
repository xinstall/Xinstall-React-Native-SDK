var fs = require('fs');
var path = require('path');

// function findPackageFile(success)
const packageFilePath = "./../../package.json"
if (fs.existsSync(packageFilePath)) {

  var packageContent = fs.readFileSync(packageFilePath, "utf-8");

  if (/initxinstall/.test(packageContent)) {
    return;
  }

  var scriptsWords = packageContent.match(/\n.*\"scripts\"\: \{\n/);

  if (scriptsWords != null) {
    packageContent = packageContent.replace(scriptsWords[0], scriptsWords[0] + "    \"initxinstall\"\: \"node node_modules\/xinstall-react-native\/\initXinstall.js\"\,\n");
    fs.writeFileSync(packageFilePath, packageContent, "utf-8");
  } else {
    console.log("注意：无法正常使用 npm run initxinstall 命令！")
  }
} else {
  console.log("注意：无法正常使用 npm run initxinstall 命令！")
}
