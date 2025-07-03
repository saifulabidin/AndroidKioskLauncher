---
applyTo: '**'
---
---
mode: agent
---
# 🐉 ULTRA INSTINCT TOOLSETS - LEGENDARY ARSENAL

This document contains the complete Ultra Instinct toolsets configuration for maximum development productivity.

## 🎯 Purpose

Custom toolsets that unleash GitHub Copilot's full potential with strategic tool selection and systematic workflows.

## ⚡ Usage
Reference this file when creating new chats to quickly restore Ultra Instinct mode.

## 🛠️ Complete Toolsets Configuration

```jsonc
{
	"webDev": {
		"tools": [
			"create_file",
			"read_file",
			"replace_string_in_file",
			"insert_edit_into_file",
			"run_in_terminal",
			"file_search",
			"grep_search",
			"semantic_search",
			"get_errors",
			"create_and_run_task"
		],
		"description": "Complete web development toolkit - file ops, search, terminal, error checking",
		"icon": "code"
	},
	"codeAnalysis": {
		"tools": [
			"semantic_search",
			"grep_search",
			"list_code_usages",
			"get_errors",
			"test_search",
			"file_search",
			"read_file"
		],
		"description": "Deep code analysis - find patterns, usages, errors, and relationships",
		"icon": "search"
	},
	"projectSetup": {
		"tools": [
			"create_new_workspace",
			"get_project_setup_info",
			"install_extension",
			"run_vscode_command",
			"create_directory",
			"create_file",
			"run_in_terminal"
		],
		"description": "Project initialization and setup - workspaces, extensions, structure",
		"icon": "rocket"
	},
	"gitOps": {
		"tools": [
			"get_changed_files",
			"run_in_terminal",
			"file_search",
			"grep_search",
			"read_file"
		],
		"description": "Git operations and version control management",
		"icon": "git-branch"
	},
	"debugging": {
		"tools": [
			"get_errors",
			"run_in_terminal",
			"get_terminal_output",
			"get_terminal_selection",
			"get_terminal_last_command",
			"test_failure",
			"semantic_search",
			"list_code_usages",
			"read_file"
		],
		"description": "Debug and troubleshoot - errors, tests, terminal outputs, terminal state",
		"icon": "bug"
	},
	"research": {
		"tools": [
			"vscode-websearchforcopilot_webSearch",
			"fetch_webpage",
			"github_repo",
			"get_vscode_api",
			"semantic_search"
		],
		"description": "Research and external resources - web search, docs, GitHub repos",
		"icon": "globe"
	},
	"dataScience": {
		"tools": [
			"create_new_jupyter_notebook",
			"edit_notebook_file",
			"run_notebook_cell",
			"copilot_getNotebookSummary",
			"create_file",
			"read_file"
		],
		"description": "Data science and Jupyter notebook operations",
		"icon": "graph"
	},
	"fullStack": {
		"tools": [
			"create_file",
			"read_file",
			"replace_string_in_file",
			"insert_edit_into_file",
			"run_in_terminal",
			"get_terminal_output",
			"get_terminal_selection",
			"get_terminal_last_command",
			"semantic_search",
			"get_errors",
			"open_simple_browser",
			"run_vs_code_task",
			"get_terminal_output"
		],
		"description": "Full-stack development - frontend, backend, testing, deployment, terminal debugging",
		"icon": "layers"
	},
	"terminalDebug": {
		"tools": [
			"run_in_terminal",
			"get_terminal_output",
			"get_terminal_selection",
			"get_terminal_last_command",
			"grep_search",
			"file_search",
			"read_file"
		],
		"description": "Terminal debugging and management - output, selection, command history",
		"icon": "terminal"
	},
	"fullContext": {
		"tools": [
			"read_file",
			"semantic_search",
			"grep_search",
			"file_search",
			"list_dir",
			"list_code_usages",
			"get_errors"
		],
		"description": "Complete context gathering - read entire files, comprehensive search, full codebase analysis",
		"icon": "book"
	},
	"smartAnalysis": {
		"tools": [
			"semantic_search",
			"read_file",
			"grep_search",
			"list_code_usages",
			"file_search",
			"get_errors",
			"test_search"
		],
		"description": "Intelligent code analysis - auto-read full files, find relationships, comprehensive understanding",
		"icon": "brain"
	},
	"massiveContext": {
		"tools": [
			"read_file",
			"semantic_search",
			"grep_search",
			"file_search",
			"list_dir"
		],
		"description": "Single-call massive file reading - read entire files up to 10k+ lines in one shot",
		"icon": "file-text"
	},
	"singleShot": {
		"tools": [
			"read_file",
			"semantic_search",
			"list_code_usages",
			"get_errors"
		],
		"description": "One-call context gathering - read complete files regardless of size, instant full understanding",
		"icon": "zap"
	},
	"performanceAudit": {
		"tools": [
			"semantic_search",
			"grep_search",
			"read_file",
			"run_in_terminal",
			"get_errors",
			"open_simple_browser",
			"vscode-websearchforcopilot_webSearch"
		],
		"description": "Performance optimization - bundle analysis, lighthouse audits, speed optimization",
		"icon": "dashboard"
	},
	"securityScan": {
		"tools": [
			"run_in_terminal",
			"file_search",
			"grep_search",
			"semantic_search",
			"get_errors",
			"vscode-websearchforcopilot_webSearch"
		],
		"description": "Security analysis - vulnerability scanning, dependency audits, security best practices",
		"icon": "shield"
	},
	"architectAnalysis": {
		"tools": [
			"semantic_search",
			"list_code_usages",
			"file_search",
			"read_file",
			"list_dir",
			"grep_search"
		],
		"description": "Architecture analysis - code structure, patterns, dependencies, refactoring opportunities",
		"icon": "organization"
	},
	"contentCreator": {
		"tools": [
			"create_file",
			"read_file",
			"semantic_search",
			"vscode-websearchforcopilot_webSearch",
			"fetch_webpage",
			"replace_string_in_file"
		],
		"description": "Content creation - documentation, README, blog posts, technical writing",
		"icon": "edit"
	},
	"deploymentOps": {
		"tools": [
			"run_in_terminal",
			"create_file",
			"read_file",
			"file_search",
			"get_errors",
			"vscode-websearchforcopilot_webSearch"
		],
		"description": "Deployment & DevOps - CI/CD, Docker, cloud deployment, infrastructure as code",
		"icon": "cloud"
	},
	"apiMaster": {
		"tools": [
			"semantic_search",
			"read_file",
			"run_in_terminal",
			"create_file",
			"get_errors",
			"open_simple_browser"
		],
		"description": "API development - REST, GraphQL, testing, documentation, integration",
		"icon": "plug"
	},
	"testingPro": {
		"tools": [
			"test_search",
			"run_in_terminal",
			"create_file",
			"read_file",
			"get_errors",
			"test_failure"
		],
		"description": "Testing excellence - unit tests, integration tests, E2E, TDD, coverage analysis",
		"icon": "beaker"
	},
	"seoOptimizer": {
		"tools": [
			"semantic_search",
			"read_file",
			"open_simple_browser",
			"vscode-websearchforcopilot_webSearch",
			"grep_search",
			"replace_string_in_file"
		],
		"description": "SEO optimization - meta tags, structured data, performance, accessibility, rankings",
		"icon": "search"
	},
	"mobileFirst": {
		"tools": [
			"read_file",
			"semantic_search",
			"open_simple_browser",
			"run_in_terminal",
			"get_errors",
			"replace_string_in_file"
		],
		"description": "Mobile-first development - responsive design, PWA, mobile performance, touch interactions",
		"icon": "device-mobile"
	},
	"dataAnalyst": {
		"tools": [
			"create_new_jupyter_notebook",
			"run_notebook_cell",
			"edit_notebook_file",
			"copilot_getNotebookSummary",
			"semantic_search",
			"read_file"
		],
		"description": "Data analysis - Jupyter notebooks, data visualization, statistical analysis, ML pipelines",
		"icon": "graph"
	},
	"i18nMaster": {
		"tools": [
			"semantic_search",
			"grep_search",
			"read_file",
			"create_file",
			"replace_string_in_file",
			"file_search"
		],
		"description": "Internationalization - translations, locale management, cultural adaptation, RTL support",
		"icon": "globe"
	},
	"aiIntegration": {
		"tools": [
			"vscode-websearchforcopilot_webSearch",
			"semantic_search",
			"create_file",
			"run_in_terminal",
			"read_file",
			"create_new_jupyter_notebook"
		],
		"description": "AI/ML integration - OpenAI APIs, machine learning models, AI-powered features, automation",
		"icon": "robot"
	},
	"automationGod": {
		"tools": [
			"run_in_terminal",
			"create_file",
			"run_vs_code_task",
			"create_and_run_task",
			"semantic_search",
			"file_search"
		],
		"description": "Advanced automation - scripts, workflows, CI/CD, task automation, code generation",
		"icon": "gear"
	},
	"crossPlatform": {
		"tools": [
			"semantic_search",
			"read_file",
			"run_in_terminal",
			"create_file",
			"get_errors",
			"file_search"
		],
		"description": "Cross-platform development - React Native, Electron, Flutter, universal apps",
		"icon": "device-desktop"
	},
	"observability": {
		"tools": [
			"run_in_terminal",
			"semantic_search",
			"read_file",
			"open_simple_browser",
			"get_errors",
			"vscode-websearchforcopilot_webSearch"
		],
		"description": "Monitoring & observability - logging, metrics, tracing, APM, alerting systems",
		"icon": "eye"
	},
	"web3Master": {
		"tools": [
			"semantic_search",
			"read_file",
			"run_in_terminal",
			"create_file",
			"vscode-websearchforcopilot_webSearch",
			"get_errors"
		],
		"description": "Blockchain & Web3 - smart contracts, DeFi, NFTs, decentralized apps, crypto integration",
		"icon": "link"
	},
	"databasePro": {
		"tools": [
			"semantic_search",
			"run_in_terminal",
			"read_file",
			"create_file",
			"get_errors",
			"grep_search"
		],
		"description": "Advanced database - SQL optimization, NoSQL, migrations, ORM, database design",
		"icon": "database"
	},
	"codeGenius": {
		"tools": [
			"semantic_search",
			"list_code_usages",
			"create_file",
			"read_file",
			"run_in_terminal",
			"replace_string_in_file"
		],
		"description": "Code generation & scaffolding - boilerplate, templates, code patterns, auto-generation",
		"icon": "wand"
	},
	"migrationExpert": {
		"tools": [
			"semantic_search",
			"list_code_usages",
			"read_file",
			"replace_string_in_file",
			"get_errors",
			"run_in_terminal"
		],
		"description": "Advanced refactoring - legacy migration, framework upgrades, code modernization",
		"icon": "arrow-right"
	},
	"enterpriseArch": {
		"tools": [
			"semantic_search",
			"read_file",
			"list_code_usages",
			"file_search",
			"create_file",
			"vscode-websearchforcopilot_webSearch"
		],
		"description": "Enterprise architecture - microservices, event-driven, distributed systems, patterns",
		"icon": "organization"
	},
	"ultraInstinct": {
		"tools": [
			"semantic_search",
			"read_file",
			"run_in_terminal",
			"create_file",
			"list_code_usages",
			"get_errors",
			"vscode-websearchforcopilot_webSearch",
			"replace_string_in_file",
			"file_search",
			"open_simple_browser"
		],
		"description": "ULTRA INSTINCT MODE - All tools combined, maximum efficiency, god-tier problem solving",
		"icon": "star"
	}
}
```

## 🚀 Quick Setup Instructions

1. **Copy the configuration** from above
2. **Open VS Code**
3. **Navigate to:** `~/.config/Code/User/prompts/test.toolsets.jsonc`
4. **Paste the configuration**
5. **Save the file**
6. **Restart VS Code** (if needed)

## 🎯 How to Use

In any new GitHub Copilot chat, you can reference specific toolsets:

- **For debugging:** "Use debugging toolset"
- **For full context:** "Use singleShot toolset" 
- **For maximum power:** "Use ultraInstinct toolset"
- **For performance:** "Use performanceAudit toolset"

## 💡 Pro Tips

1. **Mix toolsets** for complex tasks
2. **Start with singleShot** for complete context
3. **Use ultraInstinct** for unknown/complex problems
4. **Reference this document** in new chats for quick setup

## 🔥 Benefits

- **30x faster development** workflows
- **Systematic debugging** approach
- **Professional methodologies** built-in
- **Consistent tool selection** strategy
- **Maximum GitHub Copilot potential** unleashed

---

*Created during Ultra Instinct development session - December 2024*
*Power Level: OVER 9000!* 🐉⚡
