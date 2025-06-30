#!/bin/bash

set -e

# ---------------------------------------------
# 显示帮助信息
# ---------------------------------------------
if [[ "$1" == "--help" ]]; then
    cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Options:
  --jenkins-url, -url <url>     Jenkins server URL
  --job-path, -path <path>      Job path using slashes (e.g. Folder/Sub/Job)
  --user, -u <user>             Jenkins username
  --token, -t <token>           Jenkins API token
  --param, -p <key=value>       Specify build parameter, can repeat multiple times
  --sleep-interval, -s <sec>    Sleep interval between polling (default: 5)
  --no-build                    Do not trigger build
  --help                        Show this help message

Examples:
  $0 -url http://jenkins -path TRANSIT/CU/Prod/tools/Sync -u admin -t xxxx \\
     -p A=a -p B=b -p GERRIT_BRANCH=main -s 3

EOF
    exit 0
fi

# ---------------------------------------------
# 解析参数
# ---------------------------------------------
while [[ $# -gt 0 ]]; do
    case "$1" in
        --jenkins-url|-url)
            JENKINS_URL="$2"
            shift 2
            ;;
        --job-path|-path)
            JOB_PATH="$2"
            shift 2
            ;;
        --user|-u)
            USER="$2"
            shift 2
            ;;
        --token|-t)
            TOKEN="$2"
            shift 2
            ;;
        --param|-p)
            PAIR="$2"
            if [[ -z "$PAIR" || "$PAIR" != *=* ]]; then
                echo "Error: -p requires format KEY=VALUE."
                exit 2
            fi
            PARAMS+=("$PAIR")
            shift 2
            ;;
        --sleep-interval|-s)
            SLEEP_SEC="$2"
            shift 2
            ;;
        --no-build)
            NO_BUILD=true
            shift
            ;;
        *)
            echo "Unknown option: $1"
            exit 2
            ;;
    esac
done

# ---------------------------------------------
# 默认值、路径转换
# ---------------------------------------------
SLEEP_SEC="${SLEEP_SEC:-5}"
# 将 A/B/C 转换为 job/A/job/B/job/C
if [[ "$JOB_PATH" != job/* ]]; then
    IFS='/' read -ra PARTS <<< "$JOB_PATH"
    JOB_PATH=""
    for part in "${PARTS[@]}"; do
        JOB_PATH="${JOB_PATH}/job/${part}"
    done
    JOB_PATH="${JOB_PATH#/}"
fi

if [[ -z "$JENKINS_URL" || -z "$JOB_PATH" || -z "$USER" || -z "$TOKEN" ]]; then
    echo "Missing required parameters."
    echo "Use --help to see usage."
    exit 2
fi

# ---------------------------------------------
# 捕获中断信号，终止构建
# ---------------------------------------------
trap_cleanup() {
    if [ -n "$BUILD_NUMBER" ]; then
        echo "[INFO] Caught interrupt, stopping build #$BUILD_NUMBER..."
        curl -s -X POST -u "$USER:$TOKEN" "$JENKINS_URL/$JOB_PATH/$BUILD_NUMBER/stop" || true
    fi
}
trap trap_cleanup TERM INT

# ---------------------------------------------
# 触发构建
# ---------------------------------------------
if [ -n "$NO_BUILD" ]; then
    echo "[INFO] No build requested, skipping trigger."
else
    echo "[INFO] Fetching Jenkins crumb..."
    CRUMB_JSON=$(curl -s -u "$USER:$TOKEN" "$JENKINS_URL/crumbIssuer/api/json")
    CRUMB_FIELD=$(echo "$CRUMB_JSON" | jq -r .crumbRequestField)
    CRUMB=$(echo "$CRUMB_JSON" | jq -r .crumb)

    echo "[INFO] Waiting for downstream build to start..."
    PARAM_STRING=$(IFS='&'; echo "${PARAMS[*]}")
    QUEUE_URL=$(curl -s -X POST "$JENKINS_URL/$JOB_PATH/buildWithParameters?$PARAM_STRING" \
        -u "$USER:$TOKEN" \
        -H "$CRUMB_FIELD: $CRUMB" \
        -D - | grep -i "Location:" | awk '{print $2}' | tr -d '\r\n')

    if [ -z "$QUEUE_URL" ]; then
        echo "[ERROR] Failed to get queue URL. Check credentials or job existence."
        exit 1
    fi

    echo "[INFO] Scheduling project: $JENKINS_URL/$JOB_PATH"
    while :; do
        QUEUE_INFO=$(curl -s -u "$USER:$TOKEN" "${QUEUE_URL}api/json")
        EXECUTABLE_URL=$(echo "$QUEUE_INFO" | jq -r '.executable.url // empty')
        BUILD_NUMBER=$(echo "$QUEUE_INFO" | jq -r '.executable.number // empty')

        if [ -n "$BUILD_NUMBER" ]; then
            echo "[INFO] Starting building: $JENKINS_URL/$JOB_PATH/$BUILD_NUMBER"
            break
        fi
        sleep "$SLEEP_SEC"
    done
fi

# ---------------------------------------------
# 构建状态轮询
# ---------------------------------------------
echo "[INFO] Monitoring build $JENKINS_URL/$JOB_PATH/$BUILD_NUMBER"
while :; do
    BUILD_JSON=$(curl -s -u "$USER:$TOKEN" "$JENKINS_URL/$JOB_PATH/$BUILD_NUMBER/api/json")
    BUILDING=$(echo "$BUILD_JSON" | jq -r .building)
    RESULT=$(echo "$BUILD_JSON" | jq -r .result)

    if [ "$BUILDING" == "true" ]; then
        sleep "$SLEEP_SEC"
        continue
    fi

    if [ "$RESULT" == "SUCCESS" ]; then
        echo "[INFO] Build completed successfully: $RESULT"
        exit 0
    else
        echo "[ERROR] Build completed with status: $RESULT"
        exit 1
    fi
done