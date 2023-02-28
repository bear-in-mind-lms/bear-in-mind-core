# Contributing to the Bear in Mind

Hello, Honey Bear Cub. 🐻 We are glad to see you here! Below you will find a few simple rules on how to make the perfect
contribution. Your submission does not have to meet all of them to be accepted, but it will certainly speed up the
process.

## Reporting Security Vulnerabilities

If you think you have found a security vulnerability, please do not disclose it publicly until we have had a chance to
fix it. Please do not report security vulnerabilities using GitHub issues, instead contact us
at [security@kwezal.com][security]

## Issues

- Put all code and stack traces in code blocks

### Feature requests

- Describe the new feature
- Explain how the new feature will improve the project
- Come up with sample use cases (if applicable)

### Bugs

- Describe the problem
- Provide a way to reproduce the error (for complex scenarios, preferably a repository)
- Post the stack trace (if applicable)
- Describe expected behavior (if applicable)

## Pull Requests

- Before you make a pull request, please submit (or refer to) an issue
- Make sure the imports are optimized
- If you wish to document the code, try to keep the style consistent with the rest of the documentation

### Code style

To keep a consistent style of code throughout the project, we use [Prettier Java][prettier]. Before you open a pull
request, please make sure to run the following command:

```bash
mvn prettier:write
```

[security]: mailto:security@kwezal.com

[prettier]: https://github.com/HubSpot/prettier-maven-plugin