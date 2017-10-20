#!/usr/bin/env python
#encoding=utf-8
#*************************************************************************
### python delete_simple_line.py csv_file
#**功能描述：将文件夹中用户kw文件合并为csv
#**输入参数：csv_file 要求最后列为value
#**输出参数：无
#**返回值：无
#**创建者：陈东成
#**创建日期：20170925
#**修改日志：
#**
#**************************************************************************
##导入配置信息
import sys
import os

if __name__ == '__main__':

  reload(sys)
  sys.setdefaultencoding('utf-8')

  #获取参数
  csv_file = '../csv/train/classify_man.csv'#sys.argv[1]
  print 'CSV文件：' + csv_file


  new_csv = os.path.join(
      os.path.dirname(csv_file),
      'new_' + os.path.basename(csv_file)
  )
  if len(sys.argv) > 2:
      new_csv = sys.argv

  lIndex = 0
  lastValue = ''
  lastLine = ''
  skipCount = 0

  new_csv_file = open(new_csv, 'w')

  for line in open(csv_file):

      if skipCount > 10:
          print '错误数目过多，停止运行'
          exit()
      lIndex += 1
      rows = line.split('\t')

      if len(rows) != 4:
          print 'index:' + str(lIndex) + ', len:' + str(len(rows)) + ', line:' + line
          skipCount += 1
          continue

      if lIndex > 1:
          try:
              if lastValue not in rows[-1]:
                  new_csv_file.writelines(lastLine)
          except:
              print 'index:' + str(lIndex) + ', prev_text:' + lastValue + ', curr_text: ' + rows[-1]
              skipCount += 1
              continue


      lastValue = rows[-1].replace('\n', '')
      lastLine = line

  new_csv_file.writelines(lastLine)
  new_csv_file.close()

  print 'ok'