#encoding=utf-8

import urllib

license_list = [
    'classifier深度学习分类授权/deepclassifier.user',
    'Summary自动摘要提取授权/summary.user',
    'RedupRemover去重授权/LJRedupRemover.user',
    'NLPIR-ICTCLAS分词系统授权/NLPIR.user',
    'JZSearch精准搜索引擎授权/JZSearch.user',
    'HtmlParser网页正文提取授权/LJHtmlParser.user',
    'DocExtractor文档提取授权/DocExtractor.user',
    'KeyExtract关键词提取授权/keyExtract.user',
    'LJKeyScanner关键词扫描授权/keyScan.user',
    'NewWordFinder新词发现授权/NewWordFinder.user',
    'Sentiment情感分析授权/sentiment.user',
    'SplitSentence分句系统/SplitSentence.user',
    'classifier规则分类授权/classifier.user',
    'cluster聚类授权/cluster.user',
    'word2vec授权/word2Vec.user'
]

license_source = 'https://raw.githubusercontent.com/NLPIR-team/NLPIR/master/License/license%20for%20a%20month/'
save_path = './'

if __name__ == '__main__':

    i = 0
    l = len(license_list)
    for license in license_list:

        urllib.urlretrieve(license_source + license, license.split('/')[-1])
        i += 1
        print '''%s ... [OK] (%d/%d)''' % \
              (license.split('/')[-1], i, l)

    print '\nUpdate Success'

