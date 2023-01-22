provider "aws" {
  region = "us-east-1"
}

resource "aws_key_pair" "banda" {
  key_name   = "banda"
  public_key = file("~/.ssh/banda.pub")
}

resource "aws_security_group" "banda" {
  name        = "banda"
  description = "Banda security group"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_instance" "banda" {
  ami           = "ami-0ff8a91507f77f867"
  instance_type = "t2.micro"
  key_name      = aws_key_pair.banda.key_name
  security_groups = [aws_security_group.banda.name]

  connection {
    type        = "ssh"
    host        = aws_instance.banda.public_ip
    user        = "ec2-user"
    private_key = file("~/.ssh/banda")
  }

  provisioner "remote-exec" {
    inline = [
      "sudo yum install -y amazon-linux-extras",
      "sudo amazon-linux-extras enable",
      "sudo amazon-linux-extras install -y docker",
      "sudo service docker start",
      "sudo usermod -a -G docker ec2-user"
    ]
  }
}
