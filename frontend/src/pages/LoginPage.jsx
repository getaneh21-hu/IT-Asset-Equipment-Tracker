
function LoginPage({
  username,
  setUsername,
  password,
  setPassword,
  onLogin,
  loading,
  message,
}) {
  function handleSubmit(event) {
    event.preventDefault();
    onLogin(event);
  }

  return (
    <div className="login-page">
      <form className="login-card" onSubmit={handleSubmit}>
        <h1>IT Asset Tracker</h1>

        <p className="subtitle">
          Sign in to manage IT equipment
        </p>

        <label htmlFor="username">Username</label>
        <input
          id="username"
          type="text"
          autoComplete="username"
          placeholder="Enter your username"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          required
        />

        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          autoComplete="current-password"
          placeholder="Enter your password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Signing in...' : 'Log In'}
        </button>

        {message && <p role="alert">{message}</p>}

        <p className="login-note">
          Sign in with your assigned Admin or Staff account.
        </p>
      </form>
    </div>
  );
}

export default LoginPage;
