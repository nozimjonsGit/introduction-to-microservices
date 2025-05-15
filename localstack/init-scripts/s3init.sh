#!/usr/bin/env bash
awslocal s3 mb s3://staging-bucket
awslocal s3 mb s3://permanent-bucket
awslocal s3 mb s3://default-staging-bucket
awslocal s3 mb s3://default-permanent-bucket
