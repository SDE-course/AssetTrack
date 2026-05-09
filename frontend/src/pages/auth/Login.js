import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import '../../styles/auth.css';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const Login = () => {
    const navigate = useNavigate();

    const [form, setForm]       = useState({ email: '', password: '' });
    const [errors, setErrors]   = useState({});
    const [apiError, setApiError] = useState('');
    const [loading, setLoading] = useState(false);

    // ── Validation ────────────────────────────────────────────────
    const validate = () => {
        const errs = {};
        if (!form.email)                       errs.email = 'Email is required';
        else if (!/\S+@\S+\.\S+/.test(form.email)) errs.email = 'Enter a valid email';
        if (!form.password)                    errs.password = 'Password is required';
        return errs;
    };

    // ── Handlers ──────────────────────────────────────────────────
    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm(prev => ({ ...prev, [name]: value }));
        // Clear field error on type
        if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
        setApiError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const errs = validate();
        if (Object.keys(errs).length > 0) { setErrors(errs); return; }

        setLoading(true);
        setApiError('');

        try {
            const res = await fetch(`${API_BASE}/api/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: form.email, password: form.password }),
            });

            const data = await res.json();

            if (!res.ok) {
                setApiError(data.message || 'Invalid email or password');
                return;
            }

            // Store JWT and user info
            localStorage.setItem('token', data.token);
            localStorage.setItem('user', JSON.stringify({
                name: data.name,
                email: data.email,
                role: data.role,
            }));

            // Redirect to dashboard (update path when dashboard exists)
            navigate('/dashboard');

        } catch {
            setApiError('Cannot reach the server. Please try again later.');
        } finally {
            setLoading(false);
        }
    };

    // ── Render ────────────────────────────────────────────────────
    return (
        <div className="auth-page">
            <div className="auth-card login-card">

                {/* Brand */}
                <div className="auth-brand">
                    <div className="auth-brand-icon">💼</div>
                    <span className="auth-brand-name">AssetTrack</span>
                </div>

                <h1 className="auth-heading">Welcome back</h1>
                <p className="auth-subheading">Sign in to manage your assets</p>

                {/* API error */}
                {apiError && (
                    <div className="auth-alert error">{apiError}</div>
                )}

                <form onSubmit={handleSubmit} noValidate>
                    <div className="input-group">
                        <label htmlFor="email">Email</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            placeholder="you@company.com"
                            value={form.email}
                            onChange={handleChange}
                            className={errors.email ? 'input-error' : ''}
                            autoComplete="email"
                        />
                        {errors.email && <p className="field-error">{errors.email}</p>}
                    </div>

                    <div className="input-group">
                        <label htmlFor="password">Password</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            placeholder="••••••••"
                            value={form.password}
                            onChange={handleChange}
                            className={errors.password ? 'input-error' : ''}
                            autoComplete="current-password"
                        />
                        {errors.password && <p className="field-error">{errors.password}</p>}
                    </div>

                    <button type="submit" className="auth-button" disabled={loading}>
                        {loading && <span className="btn-spinner" />}
                        {loading ? 'Signing in…' : 'Sign In'}
                    </button>
                </form>

                <p className="auth-footer">
                    Don't have an account?{' '}
                    <Link to="/register" className="auth-link">Create one</Link>
                </p>
            </div>
        </div>
    );
};

export default Login;