import Vue from 'vue'
import storage from '@/common/storage'
import {
  STORAGE_KEY,
  PROBLEM_LEVEL,
  FOCUS_MODE_ROUTE_NAME
} from '@/common/constants'
import myMessage from '@/common/message'
import api from "@/common/api";
import store from '@/store'
import { PROBLEM_TYPE } from './constants';
import JSZip from 'jszip';

// function submissionMemoryFormat (memory) {
//   if (memory === undefined || memory ===null || memory === '') return '--'
//   // 1048576 = 1024 * 1024
//   let t = parseInt(memory)
//   return String(t.toFixed(0)) + 'KB'
// }
function submissionMemoryFormat(a, b) {
  if (a === null || a === '' || a === undefined) return "--";
  if (a === 0) return "0 KB";
  var c = 1024,
    d = b || 1,
    e = ["KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"],
    f = Math.floor(Math.log(a) / Math.log(c));
  return parseFloat((a / Math.pow(c, f)).toFixed(d)) + " " + e[f]
}

function submissionTimeFormat(time) {
  if (time === undefined || time === null || time === '') return '--'
  return time + 'ms'
}

function submissionLengthFormat(length) {
  if (length === undefined || length === null || length === '') return '--'
  return length + 'B'
}

function getACRate(acCount, totalCount) {
  let rate = totalCount === 0 ? 0.00 : (acCount / totalCount * 100).toFixed(2)
  return String(rate) + '%'
}

// 去掉值为空的项，返回object
function filterEmptyValue(object) {
  let query = {}
  Object.keys(object).forEach(key => {
    if (object[key] || object[key] === 0 || object[key] === false) {
      query[key] = object[key]
    }
  })
  return query
}

// 按指定字符数截断添加换行，非英文字符按指定字符的半数截断
function breakLongWords(value, length = 16) {
  let re
  if (escape(value).indexOf('%u') === -1) {
    // 没有中文
    re = new RegExp('(.{' + length + '})', 'g')
  } else {
    // 中文字符
    re = new RegExp('(.{' + (parseInt(length / 2) + 1) + '})', 'g')
  }
  return value.replace(re, '$1\n')
}

function downloadFile(url) {
  return new Promise((resolve, reject) => {
    Vue.prototype.$axios.get(url, {
      responseType: 'blob',
      timeout: 5 * 60 * 1000
    }).then(resp => {
      let headers = resp.headers
      if (headers['content-type'].indexOf('json') !== -1) {
        let fr = new window.FileReader()
        if (resp.data.error) {
          myMessage.error(resp.data.error)
        }
        fr.onload = (event) => {
          let data = JSON.parse(event.target.result)
          if (data.msg) {
            myMessage.info(data.msg)
          } else {
            myMessage.error('Invalid file format')
          }
        }
        let b = new window.Blob([resp.data], {
          type: 'application/json'
        })
        fr.readAsText(b)
        return
      }
      let link = document.createElement('a')
      link.href = window.URL.createObjectURL(new window.Blob([resp.data], {
        type: headers['content-type']
      }))
      link.download = (headers['content-disposition'] || '').split('filename=')[1]
      document.body.appendChild(link)
      link.click()
      link.remove()
      myMessage.success("Downloading...")
      resolve()
    }).catch((error) => {
      reject(error)
    })
  })
}

function downloadBoxFile(url, fileName) {
  return new Promise((resolve, reject) => {
    Vue.prototype.$axios.get(url, {
      responseType: 'blob',
      timeout: 5 * 60 * 1000
    }).then(resp => {
      let headers = resp.headers;

      // 从URL中解析出文件类型
      let fileNameWithExtension = url.split('/').pop();
      let fileType = fileNameWithExtension.split('.').pop() || 'txt';
      let filename = fileName.split('.')[0];

      headers['content-disposition'] = `attachment; filename=${filename}.${fileType}`;
      headers['content-type'] = getContentType(fileType); // 获取 MIME 类型

      let link = document.createElement('a');
      link.href = window.URL.createObjectURL(new window.Blob([resp.data], { type: headers['content-type'] }));
      link.download = (headers['content-disposition'] || '').split('filename=')[1];
      document.body.appendChild(link);
      link.click();
      link.remove();
      myMessage.success("Downloading...");
      resolve();
    }).catch((error) => {
      reject(error);
    });
  });
}


function getContentType(fileType) {
  switch (fileType.toLowerCase()) {
    case 'txt':
      return 'text/plain';
    case 'pdf':
      return 'application/pdf';
    case 'doc':
    case 'docx':
      return 'application/msword';
    case 'xls':
    case 'xlsx':
      return 'application/vnd.ms-excel';
    case 'ppt':
    case 'pptx':
      return 'application/vnd.ms-powerpoint';
    case 'jpeg':
    case 'jpg':
      return 'image/jpeg';
    case 'png':
      return 'image/png';
    case 'gif':
      return 'image/gif';
    case 'mp3':
      return 'audio/mpeg';
    case 'mp4':
      return 'video/mp4';
    case 'zip':
      return 'application/zip';
    // 添加其他常见文件类型的映射...
    default:
      return 'application/octet-stream'; // 未知类型，默认为二进制流
  }
}

function downloadFileByText(fileName, fileContent) {
  return new Promise((resolve, reject) => {
    let link = document.createElement('a')
    link.href = window.URL.createObjectURL(new window.Blob([fileContent], {
      type: 'text/plain;charset=utf-8'
    }))
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    link.remove()
    myMessage.success("Download Successfully!")
    resolve()
  })
}

async function readTestCase(problemID, name = null, fileListDir = null) {
  let url = "/api/file/download-testcase";
  if (problemID !== null || fileListDir !== null) {
    url += `?${problemID !== null ? `pid=${problemID}` : ''}${problemID !== null && fileListDir !== null ? '&' : ''}${fileListDir !== null ? `fileListDir=${fileListDir}` : ''}`;
  }
  
  let fileContents = {};
  try {
    const response = await fetch(url);

    if (!response.ok) {
      throw new Error("Failed to fetch test case. Status: " + response.status);
    }

    const blob = await response.blob();

    // 使用JSZip解压缩内容
    const zip = new JSZip();
    const zipContents = await zip.loadAsync(blob);

    // 如果没有传入name值，则返回所有有文件格式的文件的读取的文本列表
    if (name === null) {
      const filePromises = Object.values(zipContents.files).map(async (file) => {
        if (!file.dir) {
          if (file.name.split('.').length > 1) {
            const content = await file.async("text");
            fileContents[String(file.name)] = content;
          }
        }
      });

      await Promise.all(filePromises);
    } else {
      // 从zip中读取特定文件（name）的内容
      const file = zipContents.file(name);

      if (!file) {
        throw new Error("File not found in the zip archive.");
      }

      const content = await file.async("text");
      fileContents[String(name)] = content;
    }
  } catch (error) {
    fileContents = {}; // 设置默认值或根据需要进行处理
    console.error("Error reading test case:", error.message);
  }
  return fileContents;
}

function getLanguages(all = true) {
  return new Promise((resolve, reject) => {
    let languages = storage.get(STORAGE_KEY.languages)
    if (languages) {
      resolve(languages)
    } else {
      api.getAllLanguages(all).then(res => {
        let langs = res.data.data
        storage.set(STORAGE_KEY.languages, langs);
        resolve(langs);
      }, err => {
        reject(err)
      })
    }
  })
}

function stringToExamples(value) {
  let reg = "<input>([\\s\\S]*?)</input><output>([\\s\\S]*?)</output>";
  let re = RegExp(reg, "g");
  let objList = []
  let tmp;
  while (tmp = re.exec(value)) {
    objList.push({
      input: tmp[1],
      output: tmp[2]
    })
  }
  return objList
}

function examplesToString(objList) {
  if (objList.length == 0) {
    return "";
  }
  let result = ""
  for (let obj of objList) {
    result += "<input>" + obj.input + "</input><output>" + obj.output + "</output>"
  }
  return result
}

function getLevelColor(difficulty) {
  if (difficulty != undefined && difficulty != null) {
    if (PROBLEM_LEVEL[difficulty]) {
      return (
        'color: #fff !important;background-color:' +
        PROBLEM_LEVEL[difficulty]['color'] +
        ' !important;'
      );
    } else {
      return 'color: #fff !important;background-color: rgb(255, 153, 0)!important;';
    }
  }
}

function getLevelName(difficulty) {
  if (
    difficulty != undefined &&
    difficulty != null &&
    PROBLEM_LEVEL[difficulty]
  ) {
    return PROBLEM_LEVEL[difficulty]['name'][store.getters.webLanguage];
  } else {
    return 'unknown [' + difficulty + ']';
  }
}

function getTypeName(type) {
  if (
    type != undefined &&
    type != null &&
    PROBLEM_TYPE[type]
  ) {
    return PROBLEM_TYPE[type]['name'][store.getters.webLanguage];
  } else {
    return 'unknown [' + type + ']';
  }
}

function getTypeColor(type) {
  if (type != undefined && type != null) {
    if (PROBLEM_TYPE[type]) {
      return (
        'color: #fff !important;background-color:' +
        PROBLEM_TYPE[type]['color'] +
        ' !important;'
      );
    } else {
      return 'color: #fff !important;background-color: rgb(255, 153, 0)!important;';
    }
  }
}

function isFocusModePage(routeName) {
  for (let keyName in FOCUS_MODE_ROUTE_NAME) {
    if (keyName == routeName) {
      return true;
    }
  }
  return false;
}

function getFocusModeOriPage(routeName) {
  return FOCUS_MODE_ROUTE_NAME[routeName]
}

function supportFocusMode(routeName) {
  return routeName != 'ProblemDetails';
}

function getSwitchFoceusModeRouteName(routeName) {
  for (let keyName in FOCUS_MODE_ROUTE_NAME) {
    if (keyName == routeName) {
      return FOCUS_MODE_ROUTE_NAME[keyName];
    } else if (FOCUS_MODE_ROUTE_NAME[keyName] == routeName) {
      return keyName;
    }
  }
}

export default {
  submissionMemoryFormat: submissionMemoryFormat,
  submissionTimeFormat: submissionTimeFormat,
  submissionLengthFormat: submissionLengthFormat,
  getACRate: getACRate,
  filterEmptyValue: filterEmptyValue,
  breakLongWords: breakLongWords,
  downloadFile: downloadFile,
  downloadBoxFile: downloadBoxFile,
  downloadFileByText: downloadFileByText,
  getLanguages: getLanguages,
  stringToExamples: stringToExamples,
  examplesToString: examplesToString,
  getLevelColor: getLevelColor,
  getLevelName: getLevelName,
  getTypeName: getTypeName,
  getTypeColor: getTypeColor,
  isFocusModePage: isFocusModePage,
  getFocusModeOriPage: getFocusModeOriPage,
  supportFocusMode: supportFocusMode,
  getSwitchFoceusModeRouteName: getSwitchFoceusModeRouteName,
  readTestCase: readTestCase
}