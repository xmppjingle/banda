resource "aws_elastic_beanstalk_application" "banda" {
  name = "banda"
}

resource "aws_iam_role" "banda" {
  name = "banda-role"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "elasticbeanstalk.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

resource "aws_elastic_beanstalk_environment" "banda" {
  name = "banda-env"
  application = aws_elastic_beanstalk_application.banda.name
  platform = "Docker"
  service_role = aws_iam_role.banda.arn

  setting {
    namespace = "aws:elasticbeanstalk:application:environment"
    name = "REDIS_URL"
    value = "redis://username:password@host:port"
  }

  setting {
    namespace = "aws:elasticbeanstalk:application:environment"
    name = "AWS_ACCESS_KEY"
    value = "yourkey"
  }

  setting {
    namespace = "aws:elasticbeanstalk:application:environment"
    name = "AWS_SECRET_KEY"
    value = "yoursecret"
  }
}

resource "aws_security_group" "banda" {
  name = "banda"
  description = "Allow incoming traffic on port 8080"
}

resource "aws_security_group_rule" "banda_http" {
  type = "ingress"
  from_port = 8080
  to_port = 8080
  protocol = "tcp"
  cidr_blocks = ["0.0.0.0/0"]
  security_group_id = aws_security_group.banda.id
}
