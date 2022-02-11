import boto3
import urllib.request
import os

session = boto3.session.Session(
	aws_access_key_id = "ltIEKgZXRs53HtqaeiCy",
	aws_secret_access_key = "3yFz_rBb-xgkyaA_nk_V5TbnHU6DHAIFz3V8Hy6m",
	region_name = "ru-central1"
)

s3 = session.client(
	service_name='s3',
	endpoint_url='https://storage.yandexcloud.net'
)

objects = []

for key in s3.list_objects(Bucket='deepskills-datasets')['Contents']:
	objects.append(key['Key'])

BASE_URL = "https://storage.yandexcloud.net/deepskills-datasets/{}"
DEST_DIR = "programs"

for obj in objects:
	url = BASE_URL.format(obj)
	if (obj not in os.listdir()):
		urllib.request.urlretrieve(url, DEST_DIR + "/" + obj)

