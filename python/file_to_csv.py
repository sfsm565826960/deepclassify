#!/usr/bin/env python
#encoding=utf-8
#*************************************************************************
### python csv_to_file.py kw_file_path kw_csv_file
#**功能描述：将文件夹中用户kw文件合并为csv
#**输入参数：kw_csv_file 为csv文件，要求列分别为msisdn,sex,age,kw，并按msisdn进行排序
#**         output_path 文件夹路径，默认为output文件夹（要求文件夹存在）
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
import glob

if __name__ == '__main__':

  reload(sys)
  sys.setdefaultencoding('gbk')

  #获取参数
  kw_file_path = sys.argv[1]
  print 'KW目录：' + kw_file_path

  kw_csv_file = os.path.join(kw_file_path, 'output.csv')
  if len(sys.argv) > 2:
    kw_csv_file = sys.argv[2]
  print '输出CSV文件：' + kw_csv_file

  kw_file_list = glob.glob(os.path.join(kw_file_path, '*'))
  if len(kw_file_list) > 0:
    fIndex = 0
    suffix = '/' + str(len(kw_file_list)) + ')'
    csv_file = open(kw_csv_file, 'w')
    for file in kw_file_list:
      print '合并文件中...(' + str(fIndex) + suffix

      #去除前后缀，并获取信息
      info = os.path.basename(file).split('.')[0]
      if ',' in info: info = info.split(',')[1]
      info = info.decode('gbk').encode('utf-8').split('-')

      if len(info) == 3:
        for line in open(file):
          csv_file.writelines('\t'.join([
            info[2],
            info[0],
            info[1],
            line
          ]))
      elif len(info) == 2:
        for line in open(file):
          csv_file.writelines('\t'.join([
            info[1],
            info[0],
            line
          ]))
      csv_file.write('\n')
      fIndex += 1
    csv_file.close()
    print '合并完成'

  print 'OK'

