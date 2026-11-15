const listZone = document.querySelector('#task-list-zone');
const createZone = document.querySelector('#task-create-zone');
const taskList = document.querySelector('#task-list');
const form = document.querySelector('#task-form');
const formError = document.querySelector('#form-error');
const formTitle = document.querySelector('#task-form-title');
const submitTask = document.querySelector('#submit-task');
let editingTaskId = null;

function showCreateZone() {
  listZone.hidden = true;
  createZone.hidden = false;
}

function showListZone() {
  createZone.hidden = true;
  listZone.hidden = false;
}

function renderTasks(tasks) {
  taskList.replaceChildren();
  tasks.forEach((task) => {
    const item = document.createElement('li');
    item.textContent = `${task.content} / 担当者: ${task.assignee} / 状態: ${task.status} / ${task.startAt} - ${task.endAt}`;
    const editButton = document.createElement('button');
    editButton.type = 'button';
    editButton.textContent = '編集';
    editButton.addEventListener('click', () => startEdit(task));
    item.append(' ', editButton);
    if (task.canDelete) {
      const deleteButton = document.createElement('button');
      deleteButton.type = 'button';
      deleteButton.textContent = '削除';
      deleteButton.addEventListener('click', () => deleteTask(task.id));
      item.append(' ', deleteButton);
    }
    taskList.append(item);
  });
}

function startEdit(task) {
  editingTaskId = task.id;
  formTitle.textContent = 'タスク編集';
  submitTask.textContent = '更新';
  form.elements.content.value = task.content;
  form.elements.assignee.value = task.assignee;
  form.elements.status.value = task.status;
  form.elements.startAt.value = task.startAt;
  form.elements.endAt.value = task.endAt;
  showCreateZone();
}

async function csrfToken() {
  const response = await fetch('/api/tasks/csrf');
  if (!response.ok) throw new Error('操作の準備に失敗しました');
  return (await response.json()).token;
}

async function deleteTask(id) {
  formError.textContent = '';
  try {
    const response = await fetch(`/api/tasks/${id}`, {
      method: 'DELETE',
      headers: { 'X-CSRF-TOKEN': await csrfToken() }
    });
    if (!response.ok) throw new Error(response.status === 403 ? '作成者だけが削除できます' : '削除に失敗しました');
    await loadTasks();
  } catch (error) {
    formError.textContent = error.message;
  }
}

async function loadTasks() {
  const response = await fetch('/api/tasks', { cache: 'no-store' });
  if (!response.ok) throw new Error('一覧を取得できませんでした');
  renderTasks(await response.json());
}

document.querySelector('#show-create').addEventListener('click', () => {
  editingTaskId = null;
  form.reset();
  formTitle.textContent = 'タスク登録';
  submitTask.textContent = '登録';
  showCreateZone();
});
document.querySelector('#cancel-create').addEventListener('click', showListZone);
form.addEventListener('submit', async (event) => {
  event.preventDefault();
  formError.textContent = '';
  const values = Object.fromEntries(new FormData(form));
  try {
    const response = await fetch(editingTaskId === null ? '/api/tasks' : `/api/tasks/${editingTaskId}`, {
      method: editingTaskId === null ? 'POST' : 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': await csrfToken()
      },
      body: JSON.stringify(values)
    });
    if (!response.ok) throw new Error('登録に失敗しました');
    form.reset();
    editingTaskId = null;
    formTitle.textContent = 'タスク登録';
    submitTask.textContent = '登録';
    await loadTasks();
    showListZone();
  } catch (error) {
    formError.textContent = error.message;
  }
});

loadTasks().catch((error) => {
  formError.textContent = error.message;
});

setInterval(() => {
  if (!document.hidden && createZone.hidden) {
    loadTasks().catch((error) => {
      formError.textContent = error.message;
    });
  }
}, 3000);
